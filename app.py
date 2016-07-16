import tornado.ioloop
import tornado.web
import json
import os
from random import randint
from sys import maxint

all_tasks = {}
status = ['CANCELED','NOT YET DONE', 'IN PROGRESS','DONE']
class MainHandler(tornado.web.RequestHandler):
    def get(self):
        self.write("Hello, world")


class AddTaskHandler(tornado.web.RequestHandler):
    def post(self):
        def utoa(obj):
            if isinstance(obj,unicode): return obj.encode('utf-8')
            if isinstance(obj,dict):
                return {utoa(k):utoa(v) for k,v in obj.items()}
            if isinstance(obj,list): return [utoa(i) for i in obj]
            return obj
    
        self.write("Adding new Task")
        print self.request.arguments
        #task = {k:self.get_argument(k) for k in self.request.arguments}
        taskId = utoa(self.get_argument('taskId'))
        taskId =  taskId[1:-1]
        #print json.loads(taskId,encoding='utf-8')
        taskValue = self.get_argument('taskValue')
        
        
        print taskId
        print type(taskValue)
        taskValue=utoa(taskValue)
        print taskValue
        #print json.loads('"{0}"'.format(taskValue),encoding='utf-8')
        task = json.loads(taskValue)
        all_tasks[taskId] = task
        
class DoneTaskHandler(tornado.web.RequestHandler):
    def get(self):
        taskId = self.get_argument('taskId')
        all_tasks[taskId][status] = 'DONE'

class CancelTaskHandler(tornado.web.RequestHandler):
    def get(self):
        taskId = self.get_argument('taskId')
        all_tasks[taskId][status] = 'CANCELED'

class UpdateTaskHandler(tornado.web.RequestHandler):
    def get(self):
        taskId = self.get_argument('taskId')
        task = {k:self.get_argument(k) for k in self.request.arguments}
        all_tasks[taskId] = task
        self.write('Successful')
        
class DoingTaskHandler(tornado.web.RequestHandler):
    def get(self):
        taskId = self.get_argument('taskId')
        
        if all_tasks[taskId][status] == 'IN PROGRESS': return 'Already in Progress'
        else: all_tasks[taskId][status] = 'IN PROGRESS'
        self.write( 'Successful')

class PollRequestHandler(tornado.web.RequestHandler):
    def get(self):
        self.set_header("Content-Type", "application/json")
        self.write(json.dumps(all_tasks.values()))
        

def make_app():
    return tornado.web.Application([
        (r"/", MainHandler),
        (r"/addTask", AddTaskHandler),
        (r"/pollTasks", PollRequestHandler) 
    ])

if __name__ == "__main__":
    app = make_app()
    app.listen(os.getenv('PORT', 8080))
    tornado.ioloop.IOLoop.current().start()